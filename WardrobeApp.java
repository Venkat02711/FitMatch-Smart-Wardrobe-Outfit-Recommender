import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;

public class WardrobeApp extends JFrame {
    CardLayout card;
    JPanel mainPanel;
    Connection conn;

    public WardrobeApp() {
        setTitle("Smart Wardrobe Organizer");
        setSize(900,600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        card = new CardLayout();
        mainPanel = new JPanel(card);
        connectDB();
        createTables();
        insertSampleData();
        mainPanel.add(homePanel(),"HOME");
        mainPanel.add(addClothesPanel(),"ADD");
        mainPanel.add(suggestPanel(),"SUGGEST");
        mainPanel.add(laundryPanel(),"LAUNDRY");
        mainPanel.add(historyPanel(),"HISTORY");
        add(mainPanel);
        setVisible(true);
    }

    void connectDB(){
        try{
            conn=DriverManager.getConnection("jdbc:mysql://localhost:3306/wardrobe","root","");
        }catch(Exception e){JOptionPane.showMessageDialog(this,"DB Connection Failed");System.exit(0);}
    }

    void createTables(){
        try(Statement st=conn.createStatement()){
            st.execute("CREATE TABLE IF NOT EXISTS fits(id INT AUTO_INCREMENT PRIMARY KEY,name VARCHAR(50),category VARCHAR(20),color VARCHAR(20),status VARCHAR(20))");
            st.execute("CREATE TABLE IF NOT EXISTS rules(id INT AUTO_INCREMENT PRIMARY KEY,weather VARCHAR(20),occasion VARCHAR(20),day VARCHAR(20),star VARCHAR(20),preferred_category VARCHAR(20),preferred_color VARCHAR(20))");
            st.execute("CREATE TABLE IF NOT EXISTS usage_history(id INT AUTO_INCREMENT PRIMARY KEY,fit_id INT,date_worn DATE)");
            st.execute("CREATE TABLE IF NOT EXISTS laundry(id INT AUTO_INCREMENT PRIMARY KEY,fit_id INT)");
        }catch(Exception e){}
    }

    void insertSampleData(){
        try(Statement st=conn.createStatement()){
            ResultSet rs=st.executeQuery("SELECT COUNT(*) FROM rules");
            rs.next();
            if(rs.getInt(1)==0){
                st.execute("INSERT INTO rules(weather,occasion,day,star,preferred_category,preferred_color) VALUES('SUMMER','CASUAL','MONDAY','Ashwini','T-SHIRT','RED')");
                st.execute("INSERT INTO rules(weather,occasion,day,star,preferred_category,preferred_color) VALUES('WINTER','FORMAL','TUESDAY','Bharani','JACKET','BLACK')");
                st.execute("INSERT INTO rules(weather,occasion,day,star,preferred_category,preferred_color) VALUES('RAINY','PARTY','FRIDAY','Krittika','DRESS','BLUE')");
                st.execute("INSERT INTO rules(weather,occasion,day,star,preferred_category,preferred_color) VALUES('SPRING','SPORTS','SUNDAY','Rohini','PANTS','GREEN')");
                st.execute("INSERT INTO rules(weather,occasion,day,star,preferred_category,preferred_color) VALUES('SUMMER','FORMAL','WEDNESDAY','Mrigashira','SHIRT','YELLOW')");
            }
            rs=st.executeQuery("SELECT COUNT(*) FROM fits");
            rs.next();
            if(rs.getInt(1)==0){
                st.execute("INSERT INTO fits(name,category,color,status) VALUES('Red Tee','T-SHIRT','RED','AVAILABLE')");
                st.execute("INSERT INTO fits(name,category,color,status) VALUES('Black Jacket','JACKET','BLACK','AVAILABLE')");
                st.execute("INSERT INTO fits(name,category,color,status) VALUES('Blue Dress','DRESS','BLUE','AVAILABLE')");
                st.execute("INSERT INTO fits(name,category,color,status) VALUES('Green Pants','PANTS','GREEN','AVAILABLE')");
                st.execute("INSERT INTO fits(name,category,color,status) VALUES('Yellow Shirt','SHIRT','YELLOW','AVAILABLE')");
            }
        }catch(Exception e){}
    }

    JPanel homePanel(){
        JPanel p=new JPanel(new GridLayout(5,1,20,20));
        p.setBorder(new EmptyBorder(50,200,50,200));
        JButton b1=styledButton("Add Clothes",e->card.show(mainPanel,"ADD"));
        JButton b2=styledButton("Get Suggestion",e->card.show(mainPanel,"SUGGEST"));
        JButton b3=styledButton("Laundry",e->card.show(mainPanel,"LAUNDRY"));
        JButton b4=styledButton("Usage History",e->card.show(mainPanel,"HISTORY"));
        p.add(b1);p.add(b2);p.add(b3);p.add(b4);
        return p;
    }

    JButton styledButton(String text,ActionListener al){
        JButton b=new JButton(text);
        b.setFont(new Font("Segoe UI",Font.BOLD,16));
        b.setBackground(new Color(70,130,180));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(10,20,10,20));
        b.addActionListener(al);
        return b;
    }

    JPanel addClothesPanel(){
        JPanel p=new JPanel(new GridBagLayout());
        p.setBorder(new EmptyBorder(20,20,20,20));
        GridBagConstraints c=new GridBagConstraints();
        c.insets=new Insets(10,10,10,10);
        JLabel l1=new JLabel("Name");JTextField t1=new JTextField(15);
        JLabel l2=new JLabel("Category");JComboBox<String> cat=new JComboBox<>(new String[]{"T-SHIRT","SHIRT","FULL-SLEEVE","PANTS","DRESS","JACKET"});
        JLabel l3=new JLabel("Color");JComboBox<String> col=new JComboBox<>(new String[]{"LIGHT","DARK","RED","BLUE","BLACK","GREEN","YELLOW"});
        JButton save=styledButton("Save",e->{
            try(PreparedStatement ps=conn.prepareStatement("INSERT INTO fits(name,category,color,status) VALUES(?,?,?,'AVAILABLE')")){
                ps.setString(1,t1.getText());ps.setString(2,(String)cat.getSelectedItem());ps.setString(3,(String)col.getSelectedItem());
                ps.executeUpdate();JOptionPane.showMessageDialog(this,"Clothes Added");t1.setText("");
            }catch(Exception ex){}
        });
        JButton back=styledButton("Back",e->card.show(mainPanel,"HOME"));
        c.gridx=0;c.gridy=0;p.add(l1,c);c.gridx=1;p.add(t1,c);
        c.gridx=0;c.gridy=1;p.add(l2,c);c.gridx=1;p.add(cat,c);
        c.gridx=0;c.gridy=2;p.add(l3,c);c.gridx=1;p.add(col,c);
        c.gridx=0;c.gridy=3;c.gridwidth=2;p.add(save,c);
        c.gridy=4;p.add(back,c);
        return p;
    }

    JPanel suggestPanel(){
        JPanel p=new JPanel(new GridBagLayout());
        p.setBorder(new EmptyBorder(20,20,20,20));
        GridBagConstraints c=new GridBagConstraints();
        c.insets=new Insets(10,10,10,10);
        JComboBox<String> weather=new JComboBox<>(new String[]{"SUMMER","WINTER","RAINY","SPRING"});
        JComboBox<String> occasion=new JComboBox<>(new String[]{"CASUAL","FORMAL","PARTY","SPORTS"});
        JComboBox<String> day=new JComboBox<>(new String[]{"MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY","SUNDAY"});
        JComboBox<String> star=new JComboBox<>(new String[]{"Ashwini","Bharani","Krittika","Rohini","Mrigashira","Ardra","Punarvasu","Pushya","Ashlesha","Magha","PurvaPhalguni","UttaraPhalguni","Hasta","Chitra","Swati","Vishakha","Anuradha","Jyeshtha","Moola","PurvaAshadha","UttaraAshadha","Shravana","Dhanishta","Shatabhisha","PurvaBhadra","UttaraBhadra","Revati"});
        JLabel result=new JLabel("Suggestion will appear here");
        JButton get=styledButton("Get Suggestion",e->{
            try(PreparedStatement ps=conn.prepareStatement("SELECT preferred_category,preferred_color FROM rules WHERE weather=? AND occasion=? AND day=? AND star=?")){
                ps.setString(1,(String)weather.getSelectedItem());
                ps.setString(2,(String)occasion.getSelectedItem());
                ps.setString(3,(String)day.getSelectedItem());
                ps.setString(4,(String)star.getSelectedItem());
                ResultSet rs=ps.executeQuery();
                if(rs.next()){
                    String cat=rs.getString(1),col=rs.getString(2);
                    PreparedStatement ps2=conn.prepareStatement("SELECT id,name FROM fits WHERE category=? AND color=? AND status='AVAILABLE' LIMIT 1");
                    ps2.setString(1,cat);ps2.setString(2,col);
                    ResultSet rs2=ps2.executeQuery();
                    if(rs2.next()){
                        int id=rs2.getInt(1);String nm=rs2.getString(2);
                        result.setText("Wear: "+nm+" ("+cat+"-"+col+")");
                        JButton worn=styledButton("Mark as Worn",ev->{
                            try{
                                PreparedStatement ps3=conn.prepareStatement("INSERT INTO usage_history(fit_id,date_worn) VALUES(?,?)");
                                ps3.setInt(1,id);
                                ps3.setDate(2,java.sql.Date.valueOf(LocalDate.now()));
                                ps3.executeUpdate();
                                PreparedStatement ps4=conn.prepareStatement("INSERT INTO laundry(fit_id) VALUES(?)");
                                ps4.setInt(1,id);ps4.executeUpdate();
                                PreparedStatement ps5=conn.prepareStatement("UPDATE fits SET status='LAUNDRY' WHERE id=?");
                                ps5.setInt(1,id);ps5.executeUpdate();
                                JOptionPane.showMessageDialog(this,"Marked as Worn & Sent to Laundry");
                            }catch(Exception ex){}
                        });
                        p.add(worn,c);
                    } else result.setText("No available fit found");
                } else result.setText("No rule found for inputs");
            }catch(Exception ex){}
        });
        JButton back=styledButton("Back",e->card.show(mainPanel,"HOME"));
        c.gridx=0;c.gridy=0;p.add(new JLabel("Weather"),c);c.gridx=1;p.add(weather,c);
        c.gridx=0;c.gridy=1;p.add(new JLabel("Occasion"),c);c.gridx=1;p.add(occasion,c);
        c.gridx=0;c.gridy=2;p.add(new JLabel("Day"),c);c.gridx=1;p.add(day,c);
        c.gridx=0;c.gridy=3;p.add(new JLabel("Star"),c);c.gridx=1;p.add(star,c);
        c.gridx=0;c.gridy=4;c.gridwidth=2;p.add(get,c);
        c.gridy=5;p.add(result,c);
        c.gridy=6;p.add(back,c);
        return p;
    }
    JPanel laundryPanel(){
        JPanel p=new JPanel(new BorderLayout());
        DefaultListModel<String> model=new DefaultListModel<>();
        JList<String> list=new JList<>(model);
        JButton refresh=styledButton("Refresh",e->{
            model.clear();
            try(Statement st=conn.createStatement()){
                ResultSet rs=st.executeQuery("SELECT l.id,f.name FROM laundry l JOIN fits f ON l.fit_id=f.id");
                while(rs.next()) model.addElement(rs.getInt(1)+": "+rs.getString(2));
            }catch(Exception ex){}
        });
        JButton done=styledButton("Mark Laundry Done",e->{
            try(Statement st=conn.createStatement()){
                ResultSet rs=st.executeQuery("SELECT fit_id FROM laundry");
                while(rs.next()){
                    int id=rs.getInt(1);
                    PreparedStatement ps=conn.prepareStatement("UPDATE fits SET status='AVAILABLE' WHERE id=?");
                    ps.setInt(1,id);ps.executeUpdate();
                }
                st.execute("DELETE FROM laundry");
                JOptionPane.showMessageDialog(this,"Laundry Cleared");
                refresh.doClick();
            }catch(Exception ex){}
        });
        JButton back=styledButton("Back",e->card.show(mainPanel,"HOME"));
        JPanel top=new JPanel();top.add(refresh);top.add(done);top.add(back);
        p.add(top,BorderLayout.NORTH);p.add(new JScrollPane(list),BorderLayout.CENTER);
        return p;
    }
    JPanel historyPanel(){
        JPanel p=new JPanel(new BorderLayout());
        DefaultListModel<String> model=new DefaultListModel<>();
        JList<String> list=new JList<>(model);
        JButton refresh=styledButton("Refresh",e->{
            model.clear();
            try(Statement st=conn.createStatement()){
                ResultSet rs=st.executeQuery("SELECT u.id,f.name,u.date_worn FROM usage_history u JOIN fits f ON u.fit_id=f.id ORDER BY u.date_worn DESC");
                while(rs.next()) model.addElement(rs.getInt(1)+": "+rs.getString(2)+" worn on "+rs.getDate(3));
            }catch(Exception ex){}
        });
        JButton back=styledButton("Back",e->card.show(mainPanel,"HOME"));
        JPanel top=new JPanel();top.add(refresh);top.add(back);
        p.add(top,BorderLayout.NORTH);p.add(new JScrollPane(list),BorderLayout.CENTER);
        return p;
    }

    public static void main(String[] args){new WardrobeApp();}
}
